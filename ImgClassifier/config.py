import torch.optim as optim
from torchvision import models

config = {
    "run_name": "model",
    "batch_size": 64,
    "epochs": 10,
    "lr": 1e-4,
    "model": models.convnext_tiny,
    "optimizer": optim.Adam,
    "load": False,
    "model_path": "",
    "seed": 4,
    "use_lr_scheduler": True,
    "classes": [
        "baklava",
        "french_fries",
        "fried_rice",
        "hummus",
        "ice_cream",
        "pizza",
        "spring_rolls",
        "steak",
        "sushi",
        "waffles"
    ]
}
