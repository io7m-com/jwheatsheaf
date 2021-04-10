#!/bin/bash -ex

exec > >(tee build.txt) 2>&1

sudo apt-get -y install xvfb fluxbox feh ffmpeg xterm

Xvfb :99 &
XVFB_PID="$!"

export DISPLAY=:99
sleep 1

fluxbox &
sleep 1
feh --bg-tile .github/workflows/wallpaper.jpg
xterm -geometry 200x40 -e tail -F build.txt &
sleep 1
feh --bg-tile .github/workflows/wallpaper.jpg
sleep 1

ffmpeg -f x11grab -y -r 15 -video_size 1280x1024 -i :99 -vcodec libx264 test-suite.mkv &
FFMPEG_PID="$!"

"$@"

sleep 5
kill -INT "${FFMPEG_PID}"
