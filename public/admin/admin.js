// Import Firebase
import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.7.2/firebase-app.js';
import { getAuth, onAuthStateChanged, signOut, createUserWithEmailAndPassword, updateProfile } from 'https://www.gstatic.com/firebasejs/10.7.2/firebase-auth.js';
import { getFirestore, collection, getDocs, query, where, orderBy, limit, addDoc, updateDoc, doc, deleteDoc, getDoc } from 'https://www.gstatic.com/firebasejs/10.7.2/firebase-firestore.js';
import { getStorage, ref, uploadBytes, getDownloadURL } from 'https://www.gstatic.com/firebasejs/10.7.2/firebase-storage.js';

// Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyB8765432109876543210987654321098",
    authDomain: "datingapp-project.firebaseapp.com",
    projectId: "datingapp-project",
    storageBucket: "datingapp-project.appspot.com",
    messagingSenderId: "123456789012",
    appId: "1:123456789012:web:abcdef1234567890abcdef"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);
const storage = getStorage(app);

// Initialize Bootstrap components
const bsModal = new bootstrap.Modal(document.getElementById('statusModal'));
const bsBlockModal = new bootstrap.Modal(document.getElementById('blockModal'));
const bsUserModal = new bootstrap.Modal(document.getElementById('userModal'));
const bsReportModal = new bootstrap.Modal(document.getElementById('reportModal'));

// Global variables
let selectedUserId = null;
let selectedReportId = null;

// Authentication state changed listener
auth.onAuthStateChanged(user => {
    if (!user) {
        window.location.href = '../login.html';
    } else {
        // Check if user has admin role
        user.getIdTokenResult().then(idTokenResult => {
            if (!idTokenResult.claims.admin) {
                window.location.href = '../login.html';
            } else {
                loadDashboard();
                loadReports();
                loadBlocks();
                loadUsers();
            }
        });
    }
});

// Sign out
const signOutBtn = document.getElementById('signOutBtn');
if (signOutBtn) {
    signOutBtn.addEventListener('click', () => {
        signOut(auth).then(() => {
            window.location.href = '../login.html';
        });
    });
}

// Dashboard functions
async function loadDashboard() {
    const dashboardStats = {};
    
    // Get total users
    const usersSnapshot = await getDocs(collection(db, 'users'));
    dashboardStats.totalUsers = usersSnapshot.size;
    
    // Get active users (last 30 days)
    const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
    const activeUsersSnapshot = await getDocs(query(
        collection(db, 'users'),
        where('lastActive', '>=', thirtyDaysAgo)
    ));
    dashboardStats.activeUsers = activeUsersSnapshot.size;
    
    // Get total reports
    const reportsSnapshot = await getDocs(collection(db, 'reports'));
    dashboardStats.totalReports = reportsSnapshot.size;
    
    // Get total blocks
    const blocksSnapshot = await getDocs(collection(db, 'blocks'));
    dashboardStats.totalBlocks = blocksSnapshot.size;
    
    // Update dashboard display
    document.getElementById('totalUsers').textContent = dashboardStats.totalUsers;
    document.getElementById('activeUsers').textContent = dashboardStats.activeUsers;
    document.getElementById('totalReports').textContent = dashboardStats.totalReports;
    document.getElementById('totalBlocks').textContent = dashboardStats.totalBlocks;
}

// Reports functions
async function loadReports() {
    const reportsTableBody = document.getElementById('reportsTableBody');
    reportsTableBody.innerHTML = '';
    
    const reportsSnapshot = await getDocs(query(
        collection(db, 'reports'),
        orderBy('timestamp', 'desc'),
        limit(50)
    ));
    
    reportsSnapshot.forEach(doc => {
        const report = doc.data();
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${report.reporterId}</td>
            <td>${report.reportedUserId}</td>
            <td>${report.reason}</td>
            <td>${new Date(report.timestamp).toLocaleString()}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewReport('${doc.id}')">
                    View
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteReport('${doc.id}')">
                    Delete
                </button>
            </td>
        `;
        reportsTableBody.appendChild(row);
    });
}

// Blocks functions
async function loadBlocks() {
    const blocksTableBody = document.getElementById('blocksTableBody');
    blocksTableBody.innerHTML = '';
    
    const blocksSnapshot = await getDocs(collection(db, 'blocks'));
    
    blocksSnapshot.forEach(doc => {
        const block = doc.data();
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${block.blockerId}</td>
            <td>${block.blockedUserId}</td>
            <td>${new Date(block.timestamp).toLocaleString()}</td>
            <td>
                <button class="btn btn-sm btn-danger" onclick="unblockUser('${doc.id}')">
                    Unblock
                </button>
            </td>
        `;
        blocksTableBody.appendChild(row);
    });
}

// Users functions
async function loadUsers() {
    const usersTableBody = document.getElementById('usersTableBody');
    usersTableBody.innerHTML = '';
    
    const usersSnapshot = await getDocs(query(
        collection(db, 'users'),
        orderBy('createdAt', 'desc'),
        limit(100)
    ));
    
    usersSnapshot.forEach(doc => {
        const user = doc.data();
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.uid}</td>
            <td>${user.email}</td>
            <td>${user.displayName || 'N/A'}</td>
            <td>${user.phoneNumber || 'N/A'}</td>
            <td>${new Date(user.createdAt).toLocaleString()}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewUser('${doc.id}')">
                    View
                </button>
                <button class="btn btn-sm btn-warning" onclick="blockUser('${doc.id}')">
                    Block
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteUser('${doc.id}')">
                    Delete
                </button>
            </td>
        `;
        usersTableBody.appendChild(row);
    });
}

// Report actions
async function viewReport(reportId) {
    const reportDoc = await getDoc(doc(db, 'reports', reportId));
    if (reportDoc.exists()) {
        const report = reportDoc.data();
        document.getElementById('reportDetails').innerHTML = `
            <p><strong>Reporter:</strong> ${report.reporterId}</p>
            <p><strong>Reported User:</strong> ${report.reportedUserId}</p>
            <p><strong>Reason:</strong> ${report.reason}</p>
            <p><strong>Description:</strong> ${report.description || 'N/A'}</p>
            <p><strong>Timestamp:</strong> ${new Date(report.timestamp).toLocaleString()}</p>
        `;
        bsReportModal.show();
    }
}

async function deleteReport(reportId) {
    if (confirm('Are you sure you want to delete this report?')) {
        await deleteDoc(doc(db, 'reports', reportId));
        loadReports();
    }
}

// Block actions
async function blockUser(userId) {
    selectedUserId = userId;
    bsBlockModal.show();
}

async function unblockUser(blockId) {
    if (confirm('Are you sure you want to unblock this user?')) {
        await deleteDoc(doc(db, 'blocks', blockId));
        loadBlocks();
    }
}

// User actions
async function viewUser(userId) {
    const userDoc = await getDoc(doc(db, 'users', userId));
    if (userDoc.exists()) {
        const user = userDoc.data();
        document.getElementById('userDetails').innerHTML = `
            <p><strong>ID:</strong> ${user.uid}</p>
            <p><strong>Email:</strong> ${user.email}</p>
            <p><strong>Name:</strong> ${user.displayName || 'N/A'}</p>
            <p><strong>Phone:</strong> ${user.phoneNumber || 'N/A'}</p>
            <p><strong>Created:</strong> ${new Date(user.createdAt).toLocaleString()}</p>
            <p><strong>Last Active:</strong> ${new Date(user.lastActive).toLocaleString()}</p>
        `;
        bsUserModal.show();
    }
}

async function deleteUser(userId) {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
        // Delete user from authentication
        const user = await getAuth().getUser(userId);
        await deleteUser(user);
        
        // Delete user document
        await deleteDoc(doc(db, 'users', userId));
        
        // Delete any related documents
        await deleteDocsWhere('reports', 'reporterId', userId);
        await deleteDocsWhere('reports', 'reportedUserId', userId);
        await deleteDocsWhere('blocks', 'blockerId', userId);
        await deleteDocsWhere('blocks', 'blockedUserId', userId);
        
        loadUsers();
    }
}

// Helper functions
async function deleteDocsWhere(collectionName, fieldName, value) {
    const docsSnapshot = await getDocs(query(
        collection(db, collectionName),
        where(fieldName, '==', value)
    ));
    
    const batch = writeBatch(db);
    docsSnapshot.forEach(doc => {
        batch.delete(doc.ref);
    });
    await batch.commit();
}

// Update user modal
async function updateUser() {
    const userId = selectedUserId;
    const displayName = document.getElementById('updateDisplayName').value;
    const phoneNumber = document.getElementById('updatePhoneNumber').value;
    
    if (userId) {
        try {
            const userRef = doc(db, 'users', userId);
            await updateDoc(userRef, {
                displayName: displayName || null,
                phoneNumber: phoneNumber || null
            });
            
            bsUserModal.hide();
            loadUsers();
        } catch (error) {
            alert('Error updating user: ' + error.message);
        }
    }
}

// Authentication state changed listener
auth.onAuthStateChanged(user => {
    if (!user) {
        window.location.href = '../login.html';
    } else {
        loadReports();
        loadBlocks();
    }
});

// Sign out
const signOutBtn = document.getElementById('signOutBtn');
signOutBtn.addEventListener('click', () => {
    auth.signOut().then(() => {
        window.location.href = '../login.html';
    });
});

// Load reports
function loadReports() {
    const reportsTableBody = document.getElementById('reportsTableBody');
    reportsTableBody.innerHTML = '';

    db.collection('reports')
        .orderBy('timestamp', 'desc')
        .get()
        .then(snapshot => {
            snapshot.docs.forEach(doc => {
                const report = doc.data();
                const row = document.createElement('tr');
                
                row.innerHTML = `
                    <td>${report.reporterId}</td>
                    <td>${report.reportedUserId}</td>
                    <td>${report.reason}</td>
                    <td>${report.description}</td>
                    <td>
                        <span class="status-badge ${report.status}">
                            ${report.status}
                        </span>
                    </td>
                    <td>${new Date(report.timestamp).toLocaleString()}</td>
                    <td>
                        <div class="btn-group">
                            <button class="btn btn-sm btn-primary" onclick="showStatusModal('${doc.id}')">
                                Update Status
                            </button>
                            <button class="btn btn-sm btn-danger" onclick="deleteReport('${doc.id}')">
                                Delete
                            </button>
                        </div>
                    </td>
                `;
                
                reportsTableBody.appendChild(row);
            });
        });
}

// Load blocks
function loadBlocks() {
    const blocksTableBody = document.getElementById('blocksTableBody');
    blocksTableBody.innerHTML = '';

    db.collection('blocks')
        .orderBy('timestamp', 'desc')
        .get()
        .then(snapshot => {
            snapshot.docs.forEach(doc => {
                const block = doc.data();
                const row = document.createElement('tr');
                
                row.innerHTML = `
                    <td>${block.blockingUserId}</td>
                    <td>${block.blockedUserId}</td>
                    <td>${block.reason}</td>
                    <td>${new Date(block.timestamp).toLocaleString()}</td>
                    <td>
                        <button class="btn btn-sm btn-danger" onclick="unblockUser('${doc.id}')">
                            Unblock
                        </button>
                    </td>
                `;
                
                blocksTableBody.appendChild(row);
            });
        });
}

// Show status modal
function showStatusModal(reportId) {
    document.getElementById('statusModal').querySelector('#updateStatusBtn').onclick = () => {
        const status = document.getElementById('statusSelect').value;
        updateReportStatus(reportId, status);
    };
    bsModal.show();
}

// Update report status
function updateReportStatus(reportId, status) {
    db.collection('reports').doc(reportId).update({
        status: status,
        updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    }).then(() => {
        bsModal.hide();
        loadReports();
        showToast('Status updated successfully');
    }).catch(error => {
        showToast('Error updating status: ' + error.message);
    });
}

// Delete report
function deleteReport(reportId) {
    if (confirm('Are you sure you want to delete this report?')) {
        db.collection('reports').doc(reportId).delete().then(() => {
            loadReports();
            showToast('Report deleted successfully');
        }).catch(error => {
            showToast('Error deleting report: ' + error.message);
        });
    }
}

// Unblock user
function unblockUser(blockId) {
    if (confirm('Are you sure you want to unblock this user?')) {
        db.collection('blocks').doc(blockId).delete().then(() => {
            loadBlocks();
            showToast('User unblocked successfully');
        }).catch(error => {
            showToast('Error unblocking user: ' + error.message);
        });
    }
}

// Show toast message
function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    toast.style.opacity = '1';
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
