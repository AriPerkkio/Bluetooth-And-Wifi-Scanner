const EXTERNAL_SERVER_IP = 'localhost';
const EXTERNAL_SERVER_PORT = '8081';

module.exports = {
  getExtServerIp() {
    return 'http://'+ EXTERNAL_SERVER_IP +':'+ EXTERNAL_SERVER_PORT;
  }
}