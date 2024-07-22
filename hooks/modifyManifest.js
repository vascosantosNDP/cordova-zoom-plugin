#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const et = require('elementtree');

module.exports = function (context) {
    const manifestPath = path.join(context.opts.projectRoot, 'platforms', 'android', 'app', 'src', 'main', 'AndroidManifest.xml');
    
    if (fs.existsSync(manifestPath)) {
        const manifestData = fs.readFileSync(manifestPath, 'utf-8');
        const manifestTree = et.parse(manifestData);

        let modified = false;

        // Function to add attribute if it does not exist
        function addAttributeIfNotExists(element, attributeName, attributeValue) {
            if (!element.attrib[attributeName]) {
                element.attrib[attributeName] = attributeValue;
                return true;
            }
            return false;
        }

        // Function to check if attribute already exists in tools:replace
        function checkAndAddToolsReplace(element, attributeValue) {
            const toolsReplace = element.attrib['tools:replace'];
            if (toolsReplace) {
                if (!toolsReplace.split(',').includes(attributeValue)) {
                    element.attrib['tools:replace'] = toolsReplace + ',' + attributeValue;
                    return true;
                }
            } else {
                element.attrib['tools:replace'] = attributeValue;
                return true;
            }
            return false;
        }

        // Modify <provider> tag
        const providers = manifestTree.findall(".//provider[@android:authorities='${applicationId}.opener.provider']");
        providers.forEach(provider => {
            modified = checkAndAddToolsReplace(provider, 'android:authorities') || modified;
        });

        // Modify <meta-data> tag
        const metaDatas = manifestTree.findall(".//meta-data[@android:name='android.support.FILE_PROVIDER_PATHS']");
        metaDatas.forEach(metaData => {
            modified = checkAndAddToolsReplace(metaData, 'android:resource') || modified;
        });

        if (modified) {
            // Write back to AndroidManifest.xml
            const updatedManifestData = manifestTree.write({ indent: 4 });
            fs.writeFileSync(manifestPath, updatedManifestData, 'utf-8');
            console.log('AndroidManifest.xml has been updated.');
            console.log('Updated AndroidManifest.xml content:\n', updatedManifestData);
        } else {
            console.log('No modifications were necessary for AndroidManifest.xml.');
        }
    } else {
        console.warn('AndroidManifest.xml not found. Make sure the Android platform is added.');
    }
};