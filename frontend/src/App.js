import React, {useState} from 'react';
import SockJsClient from 'react-stomp';
import './App.css';
import Input from './components/Input/Input';
import LoginForm from './components/LoginForm';
import Messages from './components/Messages/Messages';
import chatAPI from './services/chatapi';
import {randomColor} from './utils/common';
import {SOCKET_URL, TOPIC} from "./utils/const";

const App = () => {
    const [messages, setMessages] = useState([])
    const [user, setUser] = useState(null)

    let onConnected = () => {
        console.log("Connected on SockJsClient !!! ")
    }
    let onDisconnect = () => {
        console.log("Disconnected  !!! ")
    }
    let onMessageReceived = (msg) => {
        console.log('New Message Received!!', msg);
        setMessages(messages.concat(msg));
        console.log(messages)
    }

    let onSendMessage = (msgText) => {
        chatAPI.sendMessage(user.username, msgText).then(res => {
            console.log('Sent', res);
        }).catch(err => {
            console.log('Error Occurred while sending message to api');
        })
    }

    let handleLoginSubmit = (username) => {
        console.log(username, " Logged in..");

        setUser({
            username: username,
            color: randomColor()
        })

    }

    return (
        <div className="App">
            {!!user ?
                (
                    <>
                        <SockJsClient
                            url={SOCKET_URL}
                            topics={[TOPIC]}
                            onConnect={onConnected}
                            onDisconnect={onDisconnect}
                            onMessage={msg => onMessageReceived(msg)}
                            debug={false}
                        />
                        <Messages
                            messages={messages}
                            currentUser={user}
                        />
                        <Input onSendMessage={onSendMessage}/>
                    </>
                ) :
                <LoginForm onSubmit={handleLoginSubmit}/>
            }
        </div>
    )
}

export default App;
