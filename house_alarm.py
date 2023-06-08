from flask import Flask, request, jsonify, send_file
import gs_main

app = Flask('house_alarm')

signal = False  # Initial signal value

# GET method - retrieve the image
@app.route('/image', methods=['GET'])
def get_image():
    
    image_path = './image.jpg'
    return send_file(image_path, mimetype='image/ppm')

# POST method - update the signal
@app.route('/signal', methods=['POST'])
def update_signal():
    global signal
    signal = request.json.get('signal', False)
    return jsonify({'signal': signal}), 201
    
@app.route('/picture', methods=['POST'])
def call_gs_main():
    gs_main.takePicture()  # Call the main function from gs_main.py
    return 'Success', 200  # Return a response with a 200 status code




# Run the application
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

