from flask import Flask, request, jsonify, send_from_directory
from model import get_label_from_model
from utils import get_recipes, nutrition_info_from_label

app = Flask(__name__)

@app.route('/upload', methods=['POST'])
def upload_image():
    if 'image' not in request.files:
        return jsonify({'msg': 'Kein Bild in der Anfrage gefunden'}), 400

    image = request.files['image']
    label = get_label_from_model(image)

    if label is None:
        return jsonify({'msg': 'Es wurde kein Gericht eindeutig erkannt!'}), 201

    nutrition_info = nutrition_info_from_label(label, request.url_root)
    nutrition_info['recipes'] = get_recipes(label, nutrition_info['calories'])

    return jsonify(nutrition_info), 200

@app.route('/imgs/<path:filename>', methods=['GET'])
def serve_img(filename):
    return send_from_directory('imgs', filename)

if __name__ == '__main__':
    app.run(debug=True)