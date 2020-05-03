# SoundRecoder
_sound recording on a microphone and sending to dropbox or google disk_

A local microphone is used where the code is executed. One recording time is 60 seconds. File name consists of current datetime.

To upload recording to Dropbox, you should have dropbox access token. How to get you can read [here](https://github.com/dropbox/dropbox-sdk-java).

To upload recording to Google Disk, you should turn on Drive API and get `client_secrets.json`, then upload it to `src/main/resources`. How to get you can read [here](https://developers.google.com/drive/api/v3/quickstart/java).


Then, fill the _app.properties_: 
- _cloud_ - `dropbox` or `google`(default = dropbox), 
- _iterationsCount_ - how many times it will make recording and send file to cloud(default = `1`),
- _dropbox_access_token_(if use Dropbox). 