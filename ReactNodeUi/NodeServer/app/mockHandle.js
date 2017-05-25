var fs = require("fs");

const WIFIPATH = 'app/Res/WifiResults.json';
const BTPATH   = 'app/Res/BluetoothResults.json';

class MockHandle {
  constructor(config) {
    this.data = {};
    this.loadData();
  }

  loadMockJson(path, key){
    fs.readFile(path, (error, data) => {
      if(error) {
        return console.log(error);
      }
      this.data[ key ] = JSON.parse(data);
    });
  }

  loadData() {
    [ {path: WIFIPATH, key: 'wifiResults' }, {path: BTPATH, key: 'bluetoothResults'} ]
      .forEach((item) => {
        this.loadMockJson(item.path, item.key);
      });
  }

  prepare(args) { }

  execute(args) { }

  fetch(args) {

    if(args.match(/count/i) && args.match(/wifi/i)) {
      return this.data.wifiResults.length;
    }
    else if(args.match(/count/i) && args.match(/bluetooth/i)) {
      return this.data.bluetoothResults.length;
    }
    else if(args.match(/wifi/i)) {
      return this.data.wifiResults;
    }
    else if (args.match(/bluetooth/i)) {
      return this.data.bluetoothResults;
    }
  }
}

module.exports = MockHandle;