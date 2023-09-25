from PIL import Image
from torch.utils.data import Dataset
import torchvision.transforms as transforms

class ImageDataset(Dataset):
    def __init__(self, image_paths, class_to_idx, config, validation) -> None:
        super().__init__()
        self.image_paths = image_paths
        self.validation = validation

        self.train_transforms = transforms.Compose([transforms.RandomHorizontalFlip(),
                                                    transforms.ToTensor(),
                                                    transforms.Normalize(
                                                        [0.485, 0.456, 0.406],
                                                        [0.229, 0.224, 0.225])])
        
        self.test_transforms = transforms.Compose([transforms.ToTensor(),
                                                   transforms.Normalize(
                                                       [0.485, 0.456, 0.406],
                                                       [0.229, 0.224, 0.225])])
        
        self._config = config
        self.class_to_idx = class_to_idx

    def __len__(self):
        return len(self.image_paths)

    def __getitem__(self, index):
        image_filepath = self.image_paths[index]
        img = Image.open(image_filepath)
        img = img.resize((128,128), Image.BICUBIC)

        if self.validation:
            image = self.train_transforms(img=img)
        else:
            image = self.test_transforms(img=img)
        
        label = image_filepath.split('/')[-2]
        label = self.class_to_idx[label]

        return image, label