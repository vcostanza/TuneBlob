![TuneBlob](app/src/main/res/mipmap-xhdpi/ic_launcher.png)

## Overview
TuneBlob is a simple Android app for aiding with musical tuning. It has two different visualization modes:
- Radial meter - Traditional radial tuning meter with a rotating pointer line that shows microtonal variation
- Graph meter - Tones are represented by lines on a grid that resembles a piano roll

Users can switch between the two modes using the left-most button on the action bar.

## Requirements
- Android 6.0 Marshmallow or newer (tested with Android 7.1, 9, and 11)
- Access to the device microphone (since this is an audio-based tuner)

## Features
- Several customizable display settings for the graph meter
- Gesture and settings-based zoom controls for the graph meter
- Audio input volume and frequency filtering
- Customizable tuning standard (440 Hz by default)
- Action bar button to switch between day and night theme

## TODO
- Add basic JUnit tests
- Option for changing the input microphone
- Split screen mode so the user can use both the radial and graph meters together

## Purpose
Besides its utility as a musical tuner (of which there are many), I created this app to learn Kotlin and brush up on my knowledge of OpenGL and C++.
This is also the first Android app I've created and released outside of a professional context. Truth be told I don't primarily consider myself an Android developer despite doing it professionally since 2015.
