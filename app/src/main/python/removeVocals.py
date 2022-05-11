import base64
import requests


def main(songBytes: str):
    url = 'http://192.168.0.177:5000/api'

    song_bytes_array = songBytes.encode('utf-8')
    decoded_song_data = base64.decodebytes(song_bytes_array)

    # print("TYPE of decoded_song_data: ")
    # print(type(decoded_song_data))

    files = {'messageFile': decoded_song_data}
    req = requests.post(url, files=files)

    print(req.status_code)
    return req.text
