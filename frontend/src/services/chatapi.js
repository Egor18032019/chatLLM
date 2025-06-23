import Axios from "axios";
import {BASE_URL_API, BASE_URL_SEND} from "../utils/const";
const api = Axios.create({
    baseURL: BASE_URL_API
});

const chatAPI = {
    // getMessages: (groupId) => {
    //     return api.get(`messages/${groupId}`);
    // },

    sendMessage: (username, text) => {
        console.log('sendMessage ' + text);
        let msg = {
            sender: username,
            content: text
        }

        return api.post(BASE_URL_SEND, msg)
    }
}


export default chatAPI;
