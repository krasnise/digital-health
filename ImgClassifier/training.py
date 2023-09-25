import torch
from glob import glob
import random
import os
import contextlib
import numpy as np
from dataset import ImageDataset
from torch.utils.data import DataLoader
from torch.cuda.amp.grad_scaler import GradScaler
from torch.utils.tensorboard import SummaryWriter


class Training:
    def __init__(self, config) -> None:
        self._device = 'cuda' if torch.cuda.is_available() else 'cpu'
        self._run_name = config['run_name']
        self._classes = config['classes']
        self.__set_seed(config['seed'])
        self.__load_dataset(config=config)
        self._model = self.__create_model(config)
        self._optimizer = self.__create_optimizer(config)
        self._scheduler = torch.optim.lr_scheduler.CyclicLR(self._optimizer, base_lr=config['lr'], max_lr=1e-5, cycle_momentum=False) if config['use_lr_scheduler'] else None
        self._criterion = torch.nn.CrossEntropyLoss()
        self._scaler = GradScaler()
        self._summary_writer = SummaryWriter(os.path.join("runs", self._run_name))

    def train_model(self, epoch):
        self._model.train()
        for inputs, labels in self._train_loader:
            inputs = inputs.to(self._device)
            labels = labels.to(self._device)
            self._optimizer.zero_grad()
            with torch.autocast(device_type=self._device, dtype=torch.float32):
                outputs = self._model(inputs)
                loss = self._criterion(outputs, labels)
            self._scaler.scale(loss).backward()
            self._scaler.step(self._optimizer)
            self._scaler.update()
            if self._scheduler is not None:
                self._scheduler.step()
        self._summary_writer.add_scalar('Loss/train', loss, epoch)
        return loss


    def test_model(self, epoch):
        self._model.eval()
        classes_cor = [0 for _ in range(len(self._classes))]
        classes_sum = [0 for _ in range(len(self._classes))]

        with torch.no_grad():
            for inputs, labels in self._valid_loader:
                inputs = inputs.to(self._device)
                labels = labels.to(self._device)
                
                outputs = self._model(inputs)

                for label, prediction in zip(labels, outputs):
                    pred = np.argmax(prediction.cpu())
                    if pred == label:
                        classes_cor[label] += 1
                    classes_sum[label] += 1

        classes_acc = [(classes_cor[idx]/classes_sum[idx])
                    * 100 for idx in range(len(self._classes))]
        mean_acc = sum(classes_acc) / len(classes_acc)
        self._summary_writer.add_scalar('Accuracy/test', mean_acc, epoch)

        return mean_acc

    def save_model(self):
        path_to_save = os.path.join('weights', self._run_name + ".pth")
        with contextlib.suppress(FileNotFoundError):
            os.remove(path_to_save)
        torch.save({
            'model_state_dict': self._model.state_dict(),
            'optimizer_state_dict': self._optimizer.state_dict(),
        }, path_to_save)

    def __create_model(self, config):
        model = config['model'](weights=True)
        if torch.cuda.is_available():
            model.cuda()

        if config['load']:
            checkpoint = torch.load(config['model_path'])
            model.load_state_dict(checkpoint['model_state_dict'])

        return model
    
    def __create_optimizer(self, config):
        optimizer = config['optimizer'](self._model.parameters(), lr=config['lr'])

        if config['load']:
            checkpoint = torch.load(config['model_path'])
            optimizer.load_state_dict(checkpoint['optimizer_state_dict'])

        return optimizer

    def __set_seed(self, seed):
        torch.manual_seed(seed)
        random.seed(seed)
        np.random.seed(seed)
        self._seed = seed


    def __load_dataset(self, config):
        g = torch.Generator()
        g.manual_seed(self._seed)
        
        train_imgs, validate_imgs = self.__load_all_files(self._classes)

        idx_to_class = {i: j for i, j in enumerate(self._classes)}
        class_to_idx = {value: key for key, value in idx_to_class.items()}

        train_dataset = ImageDataset(train_imgs, class_to_idx, config=config, validation=False)
        self._train_loader = DataLoader(train_dataset, batch_size=config['batch_size'], shuffle=True, num_workers=2, drop_last=True)

        valid_dataset = ImageDataset(validate_imgs, class_to_idx, config=config, validation=True)
        self._valid_loader = DataLoader(valid_dataset, batch_size=config['batch_size'], shuffle=False, num_workers=2)

    @staticmethod
    def __load_all_files(classes):
        dataset_path = 'data/images'

        subdirs = sorted(glob(os.path.join(dataset_path, '*')))
        subdirs = [dir for dir in subdirs if os.path.basename(dir) in classes]

        train_imgs = []
        validate_imgs = []

        for subdir in subdirs:
            image_list = sorted(glob(os.path.join(subdir, "*.jpg")))
            train_imgs.extend(image_list[:int(len(image_list)*0.9)])
            validate_imgs.extend(image_list[int(len(image_list)*0.9):])

        return train_imgs, validate_imgs