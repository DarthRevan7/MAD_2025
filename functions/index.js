const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendUserNotification = functions.firestore
  .document("users/{userId}/notifications/{notifId}")
  .onCreate(async (snap, context) => {
    const newValue = snap.data();
    const userId = context.params.userId;

    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    const fcmToken = userDoc.get("fcmToken"); // Store this in Firestore when user logs in

    if (!fcmToken) return;

    const payload = {
      notification: {
        title: "New notification!",
        body: newValue.body || "Open the app to check it out.",
      },
      token: fcmToken,
    };

    await admin.messaging().send(payload);
  });

exports.sendAndStoreNotification = functions.https.onCall(async (data, context) => {
  const { title, body } = data;

  const message = {
    notification: {
      title: title,
      body: body,
    },
    topic: 'all',
  };

  try {
    await admin.messaging().send(message);

    await admin.firestore().collection('notifications').add({
      title: title,
      body: body,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { success: true };
  } catch (error) {
    console.error('Error sending notification:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send notification');
  }
});
