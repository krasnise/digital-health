from training import Training
from config import config
import time

def start_training(config):
    highest_acc = 0

    TRAINER = Training(config=config)

    print("Start Training")
    start_time = time.time()

    for i in range(config['epochs']):
        epoch_time = time.time()
        TRAINER.train_model(i)
        result = TRAINER.test_model(i)
        print(f"Epoch: {i+1:2}, Accuracy: {result:3.2f} %, Time: {time.time()-epoch_time:3.2f}s")

        if result > highest_acc:
            highest_acc = result
            TRAINER.save_model()
    
    total_seconds = time.time() - start_time
    hours, remainder = divmod(total_seconds, 3600)
    minutes, seconds = divmod(remainder, 60)
    print(f"Training finished with {highest_acc}% Accuracy in {int(hours):02d}:{int(minutes):02d}:{int(seconds):02d} hh:mm:ss")

if __name__ == "__main__":
    start_training(config)