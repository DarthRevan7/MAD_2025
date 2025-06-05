const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

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
