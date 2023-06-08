from PIL import Image
import numpy as np
from joblib import Parallel, delayed
import gi
gi.require_version('Gst', '1.0')
gi.require_version('GLib', '2.0')
from gi.repository import  Gst,GLib

def convert_jpg_to_ppm(jpg_filename, ppm_filename):
    image = Image.open(jpg_filename)
    image.save(ppm_filename, format='PPM')
    return image

def bus_call(bus, message, loop):
    t = message.type
    if t == Gst.MessageType.EOS:
       print("End-of-stream\n")
       loop.quit()
    elif t == Gst.MessageType.ERROR:
        err, debug = message.parse_error()
        print("Error: %s: %s\n" % (err, debug))
        loop.quit()
    return True

def capture_img():
    Gst.init(None)
    pipeline_1 = 'v4l2src device=/dev/video2 num-buffers=1 ! jpegenc ! filesink location=picture1.jpg'
    pipeline_2 = 'v4l2src device=/dev/video2 num-buffers=1 ! jpegenc ! filesink location=picture2.jpg'

    pipeline1 = Gst.parse_launch(pipeline_1)
    pipeline2 = Gst.parse_launch(pipeline_2)
    # create and event loop and feed gstreamer bus mesages to it
    loop = GLib.MainLoop()

    bus = pipeline1.get_bus()
    bus.add_signal_watch()
    bus.connect("message", bus_call, loop)

    # start play back and listed to events
    pipeline1.set_state(Gst.State.PLAYING)
    loop.run()

    # cleanup
    pipeline1.set_state(Gst.State.NULL)

    loop = GLib.MainLoop()

    bus = pipeline2.get_bus()
    bus.add_signal_watch()
    bus.connect("message", bus_call, loop)

    # start play back and listed to events
    pipeline2.set_state(Gst.State.PLAYING)
    loop.run()

    # cleanup
    pipeline1.set_state(Gst.State.NULL)

    imgs = ["picture1.jpg","picture2.jpg"] 

    return imgs


# Function to read a PPM image
def read_ppm_image(filename):
    with open(filename, 'rb') as fp:
        magic = fp.readline().decode().strip()
        if magic != 'P6':
            print('Invalid PPM format')
            return None

        # Skip comments
        while True:
            line = fp.readline().decode().strip()
            if not line.startswith('#'):
                break

        width, height = map(int, line.split())
        max_val = int(fp.readline().decode().strip())

        image_size = width * height * 3
        image_data = np.fromfile(fp, dtype=np.uint8, count=image_size)
        image = np.reshape(image_data, (height, width, 3))

    return image

# Function to write a PPM image
def write_ppm_image(filename, image):
    height, width, _ = image.shape
    with open(filename, 'wb') as fp:
        fp.write(b'P6\n')
        fp.write(f'{width} {height}\n'.encode())
        fp.write(b'255\n')
        image.tofile(fp)

# Function for multifocus fusion
def multifocus_fusion(image1, image2):
    fused_image = (image1.astype(int) + image2.astype(int)) // 2
    fused_image = fused_image.astype(np.uint8)
    return fused_image

"""def new_stream_pipe():
    Gst.init(None)
    pipeline_str = 'filesrc location=/home/aykull/Documents/picture.jpg ! decodebin ! videoconvert ! jpegenc ! rtpjpegpay ! udpsink host=239.0.0.1 port=5000'
    pipeline = Gst.parse_launch(pipeline_str)
    if not pipeline:
        sys.stderr.write('could not create pipeline\n')
        sys.exit(1)
    # create and event loop and feed gstreamer bus mesages to it
    loop = GLib.MainLoop()
    bus = pipeline.get_bus()
    bus.add_signal_watch()
    bus.connect("message", bus_call, loop)
    # start play back and listed to events
    pipeline.set_state(Gst.State.PLAYING)
    try:
        loop.run()
    except:
        pass

    # cleanup
    pipeline.set_state(Gst.State.NULL)"""


def algorithm_openmp(img_1,img_2):
    image1 = read_ppm_image(img_1)
    image2 = read_ppm_image(img_2)

    if image1 is None or image2 is None:
        exit(1)
    # Parallelize the multifocus fusion
    num_cores = 4  # Specify the number of cores to use
    fused_images = Parallel(n_jobs=num_cores)(
        delayed(multifocus_fusion)(image1, image2) for _ in range(num_cores)
    )
    # Perform reduction to obtain the final fused image
    fused_image = np.mean(fused_images, axis=0).astype(np.uint8)
    write_ppm_image("fused_image.ppm", fused_image)

def algorithm_basic(img1,img2):
    image1 = read_ppm_image(img1)
    image2 = read_ppm_image(img2)

    if image1 is None or image2 is None:
        exit(1)
    fused_image = multifocus_fusion(image1, image2)
    write_ppm_image("fused_image2.ppm", fused_image)
    

def main():
    img1, img2 = capture_img()
    convert_jpg_to_ppm(img1, "out1.ppm")
    convert_jpg_to_ppm(img2, "out2.ppm")
    algorithm_openmp("out1.ppm","out2.ppm")
    algorithm_basic("out1.ppm","out2.ppm")
    

main()
    