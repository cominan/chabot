class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = [];
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 5000; // 5 seconds
    }

    connect(sessionId, onForceLogout) {
        if (this.connected) return;

        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        // Disable debug logs
        this.stompClient.debug = () => {};

        const headers = {
            'X-Session-ID': sessionId
        };

        this.stompClient.connect(headers, 
            (frame) => {
                console.log('Connected to WebSocket');
                this.connected = true;
                this.reconnectAttempts = 0;
                
                // Subscribe to force logout topic
                const subscription = this.stompClient.subscribe(
                    '/user/queue/force-logout',
                    (message) => {
                        const event = JSON.parse(message.body);
                        if (event.type === 'FORCE_LOGOUT') {
                            console.log('Received force logout event');
                            if (onForceLogout) {
                                onForceLogout(event);
                            }
                        }
                    }
                );
                this.subscriptions.push(subscription);
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                this.connected = false;
                this.handleReconnect(sessionId, onForceLogout);
            }
        );

        // Handle WebSocket close
        socket.onclose = () => {
            if (this.connected) {
                console.log('WebSocket connection closed, attempting to reconnect...');
                this.connected = false;
                this.handleReconnect(sessionId, onForceLogout);
            }
        };
    }

    handleReconnect(sessionId, onForceLogout) {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Reconnection attempt ${this.reconnectAttempts} of ${this.maxReconnectAttempts}`);
            
            setTimeout(() => {
                this.connect(sessionId, onForceLogout);
            }, this.reconnectDelay);
        } else {
            console.error('Max reconnection attempts reached');
        }
    }

    disconnect() {
        if (this.stompClient) {
            // Unsubscribe from all topics
            this.subscriptions.forEach(subscription => {
                subscription.unsubscribe();
            });
            this.subscriptions = [];
            
            // Disconnect the client
            if (this.connected) {
                this.stompClient.disconnect();
            }
            this.connected = false;
            console.log('Disconnected from WebSocket');
        }
    }

    isConnected() {
        return this.connected;
    }
}

// Create a singleton instance
const webSocketService = new WebSocketService();
export default webSocketService;
