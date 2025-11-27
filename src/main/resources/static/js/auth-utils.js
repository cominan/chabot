import webSocketService from './websocket-service';

class AuthUtils {
    constructor() {
        this.forceLogoutHandled = false;
    }

    initializeWebSocket() {
        // Get session ID from the API or from a meta tag in your HTML
        this.fetchSessionId().then(sessionId => {
            if (sessionId) {
                webSocketService.connect(sessionId, this.handleForceLogout.bind(this));
            }
        }).catch(error => {
            console.error('Failed to initialize WebSocket:', error);
        });
    }

    fetchSessionId() {
        return fetch('/api/session/id', {
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to get session ID');
            }
            return response.text();
        });
    }

    handleForceLogout(event) {
        if (this.forceLogoutHandled) return;
        this.forceLogoutHandled = true;

        // Show logout modal or notification
        this.showLogoutModal(event.message || 'This account has been logged in from another device.');

        // Disconnect WebSocket
        webSocketService.disconnect();

        // Logout the user after a delay
        setTimeout(() => {
            window.location.href = '/logout';
        }, 3000);
    }

    showLogoutModal(message) {
        // Create modal HTML if it doesn't exist
        let modal = document.getElementById('forceLogoutModal');
        
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'forceLogoutModal';
            modal.style.position = 'fixed';
            modal.style.top = '0';
            modal.style.left = '0';
            modal.style.width = '100%';
            modal.style.height = '100%';
            modal.style.backgroundColor = 'rgba(0,0,0,0.7)';
            modal.style.display = 'flex';
            modal.style.justifyContent = 'center';
            modal.style.alignItems = 'center';
            modal.style.zIndex = '9999';

            const modalContent = document.createElement('div');
            modalContent.style.backgroundColor = 'white';
            modalContent.style.padding = '20px';
            modalContent.style.borderRadius = '8px';
            modalContent.style.maxWidth = '400px';
            modalContent.style.width = '90%';
            modalContent.style.textAlign = 'center';

            const title = document.createElement('h3');
            title.textContent = 'Session Terminated';
            title.style.marginTop = '0';
            title.style.color = '#e74c3c';

            const messageEl = document.createElement('p');
            messageEl.textContent = message;
            messageEl.style.marginBottom = '20px';

            const okButton = document.createElement('button');
            okButton.textContent = 'OK';
            okButton.style.padding = '8px 20px';
            okButton.style.backgroundColor = '#e74c3c';
            okButton.style.color = 'white';
            okButton.style.border = 'none';
            okButton.style.borderRadius = '4px';
            okButton.style.cursor = 'pointer';
            okButton.onclick = () => {
                window.location.href = '/login';
            };

            modalContent.appendChild(title);
            modalContent.appendChild(messageEl);
            modalContent.appendChild(okButton);
            modal.appendChild(modalContent);
            document.body.appendChild(modal);
        }

        // Show the modal
        modal.style.display = 'flex';
    }

    cleanup() {
        this.forceLogoutHandled = false;
        webSocketService.disconnect();
    }
}

// Create a singleton instance
const authUtils = new AuthUtils();
export default authUtils;
