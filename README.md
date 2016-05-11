# home-director
Simple, browser based home automation

## Prerequisites

- a web server accessible from the internet
- a Samsung SmartThings hub and developer account

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
