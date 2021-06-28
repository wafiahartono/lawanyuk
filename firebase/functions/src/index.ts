import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';

admin.initializeApp();

const firestore = admin.firestore();

export const onReportCreated = functions.firestore.document('reports/{id}').onCreate(async (snapshot, context) => {
    const data = snapshot.data();
    if (data === undefined) return Promise.reject('data is undefined');
    const user = (await firestore.collection('users').doc(data.user.id).get()).data();
    if (user === undefined) return Promise.reject('user is undefined');
    return snapshot.ref.update({
        user: {
            full_name: user.full_name,
            id: data.user.id
        }
    });
});

export const onReportPhotoUploaded = functions.storage.object().onFinalize(async (object) => {
    const path = object?.name;
    if (path === undefined) return Promise.reject('path is undefined');
    if ((!/^reports\/[^/]+\/photos\/[^/]+$/g.test(path))) {
        console.log(`regex doesn't match, path is ${path}`)
        return Promise.resolve(null);
    }
    console.log(`regex matches, path is ${path}`)
    const reportId = path.split('/')[1];
    return firestore.collection('reports').doc(reportId).update({
        photo_urls: admin.firestore.FieldValue.arrayUnion(path)
    });
});
