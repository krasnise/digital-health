# Praktikum Digital Health SS2023

Student names: Fabian Erber, Sergej Krasnikov

## Usage

### ImgClassifier

Install [PyTorch](https://pytorch.org/get-started/locally/).

### Backend

Install requirements 

```
pip install -r requirements.txt

curl -o ngrok.zip https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip
unzip ngrok.zip
sudo mv ngrok /usr/local/bin/
```

Get API Key from this [Link](https://rapidapi.com/edamam/api/recipe-search-and-diet) and paste it into line 28 in the utils.py

```
headers = {
    "Accept-Language": "en",
    "X-RapidAPI-Key": "<Your API Key>",
    "X-RapidAPI-Host": "edamam-recipe-search.p.rapidapi.com"
}
```

Start Flask-Server

```
python server.py
```

Start ngrok

```
ngrok http 5000
```

Copy forwarded address, e.g. https://cb25-62-216-204-163.ngrok.io

```
ngrok                                                                                                                                                              (Ctrl+C to quit)
                                                                                                                                                                                   
Take our ngrok in production survey! https://forms.gle/aXiBFWzEA36DudFn6                                                                                                           
                                                                                                                                                                                   
Session Status                online                                                                                                                                               
Session Expires               1 hour, 59 minutes                                                                                                                                   
Update                        update available (version 3.3.4, Ctrl-U to update)                                                                                                   
Terms of Service              https://ngrok.com/tos                                                                                                                                
Version                       3.3.1                                                                                                                                                
Region                        Europe (eu)                                                                                                                                          
Latency                       12ms                                                                                                                                                 
Web Interface                 http://127.0.0.1:4040                                                                                                                                
Forwarding                    https://cb25-62-216-204-163.ngrok.io -> http://localhost:5000                                                                                        
                                                                                                                                                                                   
Connections                   ttl     opn     rt1     rt5     p50     p90                                                                                                          
                              0       0       0.00    0.00    0.00    0.00   
```

## Pomodoro2

Open the App in Android Studio and open the MainActivity in app/java/com.example.pomodoro2/ and paste the forwarded address into line 235. 

```
val request = Request.Builder()
    .url("<Your ngrok address>/upload")
    .post(requestBody)
    .build()
```

Install App as usual on the device and use.