## NOTE: The instructions below were based on Google Sample Project smart-home-java

# Java Smart Home Sample with MQTT interface to connect to IOT devices (ESP8266/Arduino/ESP32)

This sample adaptation with MQTT is to help you get started quickly with the Java smart home library for Actions on Google.
This example was used in the tutorial : How to connect ESP8266 and Arduino with Google Assistant and Google Home App without IFTTT
https://youtu.be/0czm7VIgoZs - Part 2
https://youtu.be/-tnYAI3mE24 - Part 1 

## Setup Instructions

### Clone this project (my-smart-home-java-mqtt)
1. Clone this github project at your machine. git clone https://github.com/electrofun-smart/my-smart-home-java-mqtt.git
1. By using an IDEA (recommended Intellij) import and build the project.
1. Install Google Cloud SDK in your machine

### Create an Smart Home Action Project

1. Use the [Actions on Google Console](https://console.actions.google.com), click on a **New project** and add a project name and click **Create Project**.
1. Click **Smart Home** as a type of action and **Start building**.
1. Then add a name of your Smart Home action, in the **Display Name**, it will show latter in Android app Google Home as a brand service, then click **Save**.
1. Click on **Actions** at left Menu
1. Enter the URL for fulfillment, don forget to change the url with your project id, e.g. https://your_project_id.appspot.com/smarthome, click **Save**.
1. From the left menu click on **Account Linking**.
1. Under Client Information, add anything on the client ID and secret, it will not be used on this sample.
1. The Authorization URL is the hosted URL of your app with '/fakeauth' as the path, e.g. https://your_project_id.appspot.com/fakeauth
1. The Token URL is the hosted URL of your app with '/faketoken' as the path, e.g. https://your_project_id.appspot.com/faketoken
1. Then click **Save**
1. On the top navigation menu under **Test**, click on **Talk to 'Display name'**, to begin testing this app. Obs. An error will be returned but this is enough to have the service available o Google Home for test and personal use.

### Credentials and API enabling
To fully utilize the features of this project, including the Report State API, you must setup account credentials.
1. Navigate to the [Google Cloud Console API & Services page](https://console.cloud.google.com/apis/credentials)
2. Be sure you are currently inside your project (view dropdown on the top of the page)
1. Select **Create Credentials** and create a **Service account key**
1. Create the account and download a JSON file. Save this file at you cloned project in the resource location at `src/main/resources/smart-home-key.json`.
1. Enable cloud billing in the project https://console.developers.google.com/apis/api/cloudbuild.googleapis.com/overview?project=your-project-id
2. Note that you need to create a billing account with an credit card, usually Google offers 300USD to be used in G Cloud applications. If your project is small and will not have too much requests on Google end-points the costs is very low per month. But be carefull, I recommend also that you set limits per month to avoid surprises. 
1. Enable Google Home Graph API https://console.cloud.google.com/apis/api/homegraph.googleapis.com/overview?project=your-project-id

### Connect to Firebase

1. Open your project in the Firebase console (https://console.firebase.google.com/), click on the left menu in **Database** and **Create Database**.
1. Start in production Mode, choose the cloud firestore location that better fits your location, click **Done**
1. Click on **Rules** and edit as code below and click on **Publish**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read;
      allow write: if false;
    }
  }
}

``` 
1. Configure a `users` collection with a default user and a few default fields that match exactly:
2. Click **Start Collection** add users, click **Next**. Documment Id add 1234 and **Save**. Below 1234 click on **Add field** and add the 3 fields below where first 2 as String and homegraph as boolean. 

```
    users\
        1234
            fakeAccessToken: "123access"
            fakeRefreshToken: "123refresh"
            homegraph: false
```

### Deploying the project
1. At Intellij with your project opened, open a terminal or Intellij terminal, type the command **gcloud init** (from Google SDK) and follow the options to select your user and project. At first time a url will be given to complete the user authentication.
2. Then type command **gcloud app create** and select the same location as you did on Firestore database. (it must be the same otherwise you will need to re-create the project).
1. At Intellij open the Gradle box (top right corner) under Tasks->app engine standard environment, double click on **appengineDeploy**
1. Your project will be deployed at Google Cloud and the url will be prompt at the end of the building. 
1. Double check and update your project if needed at https://console.actions.google.com with url where app was deployed from step above. (Fulfillment URL and Authorization and Token Urls)

#### Setup Account linking

1. On a device with the Google Assistant logged into the same account used
to create the project in the Actions Console, enter your Assistant settings.
1. Click Home Control.
1. Click the '+' sign to add a device.
1. Find your project display name in the list of providers.
1. Log in to your service.
1. Nothing happens because there is no devices created yet. Next step I will show how to install frontend application to create devices.

### Setup front-end applicatin
1. You can also follow the setup for a local frontend to test adding and testing devices
 here: git clone https://github.com/actions-on-google/smart-home-frontend.git

Assistant will only provide you control over items that are registered, so if you visit your front
end and click the add icon to create a device your server will receive a
new SYNC command.



### References & Issues

#### Build for Google Cloud Platform

   1. Instructions for [Google Cloud App Engine Standard Environment](https://cloud.google.com/appengine/docs/standard/java/)
    1. Use gcloud CLI to set the project to the name of your Actions project. Use 'gcloud init' to initialize and set your Google cloud project to the name of the Actions project.
    1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle) by running the following command: `gradle appengineDeploy`. You can do this directly from
    IntelliJ by opening the Gradle tray and running the appEngineDeploy task. This will start the process to deploy the fulfillment code to Google Cloud App Engine.

+ Questions? Go to [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google), [Assistant Developer Community on Reddit](https://www.reddit.com/r/GoogleAssistantDev/) or [Support](https://developers.google.com/assistant/support).
+ For bugs, please report an issue on Github.
+ Actions on Google [Documentation](https://developers.google.com/assistant)
+ Actions on Google [Codelabs](https://codelabs.developers.google.com/?cat=Assistant).

