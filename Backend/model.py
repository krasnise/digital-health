import torch
import torch.nn as nn
from PIL import Image
from io import BytesIO
import torchvision.transforms as transforms
import sys
import os

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from ImgClassifier.config import config


def get_label_from_model(image_file):
    model = create_model()

    img_io = BytesIO()
    image_file.save(img_io)
    img_io.seek(0)

    image = Image.open(img_io)

    transformed_image = transform_img(image)
    transformed_image = transformed_image.unsqueeze(0)

    output = model(transformed_image)
    int_class = torch.argmax(output, dim=1)
    int_class.detach()

    output = output.detach().squeeze(0)
    confidence = torch.max(torch.softmax(output, dim=-1))

    print(confidence)

    if confidence < 0.8:
        return None
    
    str_label = config['classes'][int_class]
    return str_label

def create_model():
    model = config['model'](weights=True)
    model.classifier[2] = nn.Linear(in_features=768, out_features=len(config['classes']), bias=True)

    weights_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "ImgClassifier", "weights")
    checkpoint = torch.load(os.path.join(weights_path, "model.pth"), map_location=torch.device('cpu'))
    model.load_state_dict(checkpoint['model_state_dict'])

    return model

def transform_img(image):
    image = image.resize((128,128), Image.BICUBIC)
    image = image.rotate(-90, expand=True)

    transform = transforms.Compose([transforms.ToTensor(),
                                     transforms.Normalize(
                                         [0.485, 0.456, 0.406],
                                         [0.229, 0.224, 0.225])])
    
    
    return transform(image)
