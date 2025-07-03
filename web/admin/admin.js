// Firebase configuration - replace with your actual config
const firebaseConfig = {
    apiKey: "YOUR_API_KEY",
    authDomain: "YOUR_AUTH_DOMAIN",
    projectId: "YOUR_PROJECT_ID",
    storageBucket: "YOUR_STORAGE_BUCKET",
    messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
    appId: "YOUR_APP_ID"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

const db = firebase.firestore();
const auth = firebase.auth();

// Initialize Bootstrap components
const bsModal = new bootstrap.Modal(document.getElementById('statusModal'));
const bsBlockModal = new bootstrap.Modal(document.getElementById('blockModal'));

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
