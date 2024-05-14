#!/bin/bash -ex
#
#  Automatically generated: DO NOT EDIT.
#
#  Generation code: https://www.github.com/io7m-com/.github/
#

exec > >(tee build.txt) 2>&1

#---------------------------------------------------------------------
# Install all of the various required packages.
#
# We use:
#   xvfb    to provide a virtual X server
#   fluxbox to provide a bare-minimum window manager with click-to-focus
#   ffmpeg  to record the session
#   feh     to set a background
#

sudo apt-get -y update
sudo apt-get -y upgrade
sudo apt-get -y install xvfb fluxbox feh ffmpeg

#---------------------------------------------------------------------
# Start Xvfb on a new display.
#

Xvfb :99 &
export DISPLAY=:99
sleep 1

#---------------------------------------------------------------------
# Start recording the session.
#

ffmpeg -f x11grab -y -r 60 -video_size 1280x1024 -i :99 -vcodec vp9 test-suite.webm &
FFMPEG_PID="$!"

#---------------------------------------------------------------------
# Start fluxbox on the X server.
#

fluxbox &
sleep 1

#---------------------------------------------------------------------
# Set a desktop image.
#

feh --bg-tile .github/workflows/wallpaper.png
sleep 1

#---------------------------------------------------------------------
# Execute the passed-in build command.
#

"$@"

#---------------------------------------------------------------------
# Wait a while, and then instruct ffmpeg to stop recording. This step
# is necessary because video files need to be processed when recording
# stops.
#

sleep 20
kill -INT "${FFMPEG_PID}" || true
