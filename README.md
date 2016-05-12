# Home Director

Simple, self-hosted, browser based home automation

![dashboard screenshot](https://s3-eu-west-1.amazonaws.com/home-director/github-screenshot.png)

## Current status

- Supplies a self-hosted smart home server able to take updates from Samsung SmartThings
- Comes with code to install as a SmartThings Smart App
- Has a simple view-only dashboard showing the status of your devices
- Dashboard updates in real-time
- Works on Chrome and iOS Safari as a minimum

## Prerequisites

- a web server accessible from the internet
- a Samsung SmartThings hub and developer account

## Security warning

This project is not yet ready for use. It should be considered pre-alpha quality. Use it at your own risk.

The code does not yet supply any security whatsoever. It is highly recommended to supply your own (not self-signed) HTTPS certificate for your server. Once installed on an internet facing server (required for SmartThings to talk to it) anyone with knowledge of the URL will be able to view your dashboard. Furthermore, updates from SmartThings are not yet validated in any way and are open to spoofing.

Future security goals include:
- Plugin-based user authentication, probably using Node passport
- Secure communications from SmartThings using server-side secret tokens

## Getting started

- Clone this repo and `npm install`
- Add and publish the contents of the `smart-app.groovy` to your Samsung SmartThings SmartApps
- Add the SmartApp in your SmartThings phone app and add the devices you wish to manage
- In the SmartApp config:
  - set the Director URL to be your externally accessible URL, suffixed with `/api/0/update`. For example: `http://myserver/api/0/update`
  - Tap the "Config" section, copy and send the generated JSON to your server
- In the root of the Home Director repo, create a `.env` file
- In the `.env` file add a `SMARTTHINGS_URL=https://[app_url]/[app_id]/devices?access_token=[access_token]` line, filling in the placeholder values
  with the JSON config you copied from the app
- `npm run dev` to start the development server. Set the `PORT` environment variable to whichever port is externally accessible from the internet
