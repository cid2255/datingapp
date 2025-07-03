const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { Messaging } = require('@google-cloud/messaging');

admin.initializeApp();
const messaging = new Messaging();

// Firestore paths
const MESSAGES_PATH = 'messages/{messageId}';
const CALLS_PATH = 'calls/{callId}';
const MATCHES_PATH = 'matches/{matchId}';
const USERS_PATH = 'users/{userId}';

// Message trigger
exports.onMessageCreated = functions.firestore
    .document(MESSAGES_PATH)
    .onCreate(async (snap, context) => {
        const message = snap.data();
        const senderId = message.senderId;
        const receiverId = message.receiverId;
        const matchId = message.matchId;

        // Get sender info
        const senderDoc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', senderId)}`).get();
        const sender = senderDoc.data();

        // Get receiver FCM token
        const receiverDoc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', receiverId)}`).get();
        const receiver = receiverDoc.data();

        // Send notification to receiver
        const messageNotification = {
            notification: {
                title: sender.displayName || 'New Message',
                body: message.text,
                icon: sender.photoURL,
            },
            data: {
                type: 'message',
                senderId: senderId,
                senderName: sender.displayName,
                matchId: matchId,
            },
            token: receiver.fcmToken,
        };

        try {
            await messaging.send(messageNotification);
            console.log('Message notification sent successfully');
        } catch (error) {
            console.error('Error sending message notification:', error);
        }

        // Update match's last message info
        await admin.firestore().doc(`${MATCHES_PATH.replace('{matchId}', matchId)}`).update({
            lastMessage: message.text,
            lastMessageTime: admin.firestore.FieldValue.serverTimestamp(),
            unreadCount: admin.firestore.FieldValue.increment(1),
        });
    });

// Call trigger
exports.onCallCreated = functions.firestore
    .document(CALLS_PATH)
    .onCreate(async (snap, context) => {
        const call = snap.data();
        const callerId = call.callerId;
        const receiverId = call.receiverId;

        // Get caller info
        const callerDoc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', callerId)}`).get();
        const caller = callerDoc.data();

        // Get receiver FCM token
        const receiverDoc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', receiverId)}`).get();
        const receiver = receiverDoc.data();

        // Send notification to receiver
        const callNotification = {
            notification: {
                title: caller.displayName || 'Incoming Call',
                body: 'You have an incoming call',
                icon: caller.photoURL,
            },
            data: {
                type: 'call',
                callerId: callerId,
                callerName: caller.displayName,
            },
            token: receiver.fcmToken,
        };

        try {
            await messaging.send(callNotification);
            console.log('Call notification sent successfully');
        } catch (error) {
            console.error('Error sending call notification:', error);
        }
    });

// Match trigger
exports.onMatchCreated = functions.firestore
    .document(MATCHES_PATH)
    .onCreate(async (snap, context) => {
        const match = snap.data();
        const userId1 = match.user1;
        const userId2 = match.user2;

        // Get user info
        const user1Doc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', userId1)}`).get();
        const user2Doc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', userId2)}`).get();
        const user1 = user1Doc.data();
        const user2 = user2Doc.data();

        // Send notification to both users
        const matchNotification = {
            notification: {
                title: 'New Match',
                body: 'You have a new match!',
                icon: user1.photoURL || user2.photoURL,
            },
            data: {
                type: 'match',
                matchId: context.params.matchId,
            },
        };

        try {
            // Send to user1
            if (user1.fcmToken) {
                await messaging.send({
                    ...matchNotification,
                    token: user1.fcmToken,
                });
            }

            // Send to user2
            if (user2.fcmToken) {
                await messaging.send({
                    ...matchNotification,
                    token: user2.fcmToken,
                });
            }

            console.log('Match notifications sent successfully');
        } catch (error) {
            console.error('Error sending match notifications:', error);
        }
    });

// User trigger for FCM token updates
exports.onUserUpdated = functions.firestore
    .document(USERS_PATH)
    .onUpdate(async (change, context) => {
        const oldData = change.before.data();
        const newData = change.after.data();

        // If FCM token changed
        if (oldData.fcmToken !== newData.fcmToken) {
            // Update any existing notifications
            const matches = await admin.firestore()
                .collection('matches')
                .where('user1', '==', context.params.userId)
                .get();

            const calls = await admin.firestore()
                .collection('calls')
                .where('receiverId', '==', context.params.userId)
                .get();

            // Update match notifications
            matches.forEach(async (match) => {
                const matchData = match.data();
                const otherUserId = matchData.user1 === context.params.userId ? matchData.user2 : matchData.user1;
                
                const otherUserDoc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', otherUserId)}`).get();
                const otherUser = otherUserDoc.data();

                // Send updated notification
                const messageNotification = {
                    notification: {
                        title: 'New Match',
                        body: 'You have a new match!',
                        icon: otherUser.photoURL,
                    },
                    data: {
                        type: 'match',
                        matchId: match.id,
                    },
                    token: newData.fcmToken,
                };

                try {
                    await messaging.send(messageNotification);
                } catch (error) {
                    console.error('Error updating match notification:', error);
                }
            });

            // Update call notifications
            calls.forEach(async (call) => {
                const callData = call.data();
                const callerDoc = await admin.firestore().doc(`${USERS_PATH.replace('{userId}', callData.callerId)}`).get();
                const caller = callerDoc.data();

                // Send updated notification
                const callNotification = {
                    notification: {
                        title: caller.displayName || 'Incoming Call',
                        body: 'You have an incoming call',
                        icon: caller.photoURL,
                    },
                    data: {
                        type: 'call',
                        callerId: callData.callerId,
                        callerName: caller.displayName,
                    },
                    token: newData.fcmToken,
                };

                try {
                    await messaging.send(callNotification);
                } catch (error) {
                    console.error('Error updating call notification:', error);
                }
            });
        }
    });
