/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

// [START all]
// [START import]
// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
// [END import]

/**
 * Triggers when a user gets a new follower and sends a notification.
 *
 * bloodposts add a flag to `/bloodposts/{postId}/donorList/{donorUid}`.
 * Users save their device notification tokens to `/users/{authorUid}/notificationTokens/{notificationToken}`.
 */
exports.sendBloodNotification = functions.database.ref('/user-bloodposts/{postUid}/{postId}/donorCount').onWrite(event => {
  const postUid = event.params.postUid;
  const postId = event.params.postId;
  
 // var eventSnapshot = event.data.child('profilePicture').val();
  
  const donorCountChange = event.data.val() - event.data.previous.val();
   console.log('donorCountChange', donorCountChange);
  // Exit if no change in donorList
  if (donorCountChange == 0) {
  return null;
  }

  //console.log('We have a new follower UID:', postUid, 'for user:', postUid);

  // Get the list of device notification tokens.
  const getDeviceTokensPromise = admin.database().ref(`/users/${postUid}/notificationTokens`).once('value');

  return Promise.all([getDeviceTokensPromise]).then(results => {
    const tokensSnapshot = results[0];

    // Check if there are any device tokens.
    if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
    console.log('Fetched postId', postId);

    // Notification details.
    const payload = {
      notification: {
        title: 'Your donor list has just changed',
        body: 'Tap to see details!',
		click_action: 'net.givelives.givelives.blood.BloodRequestDetailActivity'
      },
	data: {
    'post_key': postId,
    'post_uid': postUid
  }
  };

    // Listing all tokens.
    const tokens = Object.keys(tokensSnapshot.val());

    // Send notifications to all tokens.
    return admin.messaging().sendToDevice(tokens, payload).then(response => {
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
    });
  });
});
// [END all]

/**
 * Triggers when a user gets a new follower and sends a notification.
 *
 * bloodposts add a flag to `/bloodposts/{postId}/donorList/{donorUid}`.
 * Users save their device notification tokens to `/users/{authorUid}/notificationTokens/{notificationToken}`.
 */
exports.sendCommentNotification = functions.database.ref('/post-comments/{postId}/{commentId}/postUid').onCreate(event => {
  
  const postId = event.params.postId;
  // const commentId = event.params.commentId;
  const postUid =  event.data.val();

  // Get the list of device notification tokens.
  const getDeviceTokensPromise = admin.database().ref(`/users/${postUid}/notificationTokens`).once('value');
  // const getCommentsPromise = admin.database().ref(`/post-comments/${postId}`).once('value');
 
  return Promise.all([getDeviceTokensPromise]).then(results => {
    const tokensSnapshot = results[0];
//	const commentsSnapshot = results[1];

    // Check if there are any device tokens.
    if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }
	  console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
    console.log('Fetched postId', postId);
  
    // Notification details.
    const payload = {
      notification: {
        title: 'Somebody just commented on your post',
        body: 'Tap to see details!',
		click_action: 'net.givelives.givelives.blood.BloodRequestDetailActivity'
      },
	data: {
    'post_key': postId,
    'post_uid': postUid
  }
  };

 // Listing all tokens.
    const tokens = Object.keys(tokensSnapshot.val());

    // Send notifications to all tokens.
    return admin.messaging().sendToDevice(tokens, payload).then(response => {
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
    });
  });
});
// [END all]


exports.sendOrganNotification = functions.database.ref('/user-organposts/{postUid}/{postId}/donorCount').onWrite(event => {
  const postUid = event.params.postUid;
  const postId = event.params.postId;
  
 // var eventSnapshot = event.data.child('profilePicture').val();
  
  const donorCountChange = event.data.val() - event.data.previous.val();
   console.log('donorCountChange', donorCountChange);
  // Exit if no change in donorList
  if (donorCountChange == 0) {
  return null;
  }

  //console.log('We have a new follower UID:', postUid, 'for user:', postUid);

  // Get the list of device notification tokens.
  const getDeviceTokensPromise = admin.database().ref(`/users/${postUid}/notificationTokens`).once('value');

  return Promise.all([getDeviceTokensPromise]).then(results => {
    const tokensSnapshot = results[0];

    // Check if there are any device tokens.
    if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
    console.log('Fetched postId', postId);

    // Notification details.
    const payload = {
      notification: {
        title: 'Your donor list has just changed',
        body: 'Tap to see details!',
		click_action: 'net.givelives.givelives.organ.OrganRequestDetailActivity'
      },
	data: {
    'post_key': postId,
    'post_uid': postUid
  }
  };

    // Listing all tokens.
    const tokens = Object.keys(tokensSnapshot.val());

    // Send notifications to all tokens.
    return admin.messaging().sendToDevice(tokens, payload).then(response => {
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
    });
  });
});
// [END all]