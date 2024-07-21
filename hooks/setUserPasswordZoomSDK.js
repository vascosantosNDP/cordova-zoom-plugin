#!/usr/bin/env node
const fs = require('fs');
const path = require('path');
const xml2js = require('xml2js');
const glob = require('glob');

const args = process.argv;
let password;

for (const arg of args) {  
  if (arg.includes('ANDROID_ZOOM_SDK_PASSWORD')) {
    const stringArray = arg.split('=');
    password = stringArray.slice(-1).pop();
    console.log('Value from ANDROID_ZOOM_SDK_PASSWORD: ' + password);
  }
}

const variables = {
  ANDROID_ZOOM_SDK_PASSWORD: password || 'ANDROID_ZOOM_SDK_PASSWORD'
};

module.exports = function (context) {
  const configXmlPath = path.join(context.opts.projectRoot, 'config.xml');

  fs.readFile(configXmlPath, 'utf-8', function (err, data) {
    if (err) throw new Error('Unable to find config.xml: ' + err);

    xml2js.parseString(data, function (err, result) {
      if (err) throw new Error('Unable to parse config.xml: ' + err);

      console.log('Parsed config.xml:', JSON.stringify(result, null, 2)); // Log do objeto resultante

      try {
        const platforms = result.widget.platform;
        let sdkPassword = '';

        if (platforms) {
          platforms.forEach(function (platform) {
            if (platform.$.name === 'android') {
              const preferences = platform.preference;
              if (preferences) {
                preferences.forEach(function (pref) {
                  if (pref.$.name === 'ANDROID_ZOOM_SDK_PASSWORD') {
                    sdkPassword = pref.$.value || '';
                  }
                });
              }
            }
          });
        }

        sdkPassword = variables.ANDROID_ZOOM_SDK_PASSWORD;

        if (!sdkPassword) {
          throw new Error('SDK password preferences not found in config.xml');
        }

        // Buscar todos os arquivos .gradle no projeto
        const gradleFilesPattern = path.join(context.opts.projectRoot, '**', '*.gradle');
        glob(gradleFilesPattern, function (err, files) {
          if (err) throw new Error('Error finding .gradle files: ' + err);

          files.forEach(function (file) {
            let gradleData = fs.readFileSync(file, 'utf-8');
            gradleData = gradleData.replace('ANDROID_ZOOM_SDK_PASSWORD', sdkPassword);

            fs.writeFileSync(file, gradleData, 'utf-8');
            console.log(`Updated ${file} with actual SDK credentials.`);
          });
        });
      } catch (e) {
        console.error('Error processing config.xml:', e);
      }
    });
  });
};