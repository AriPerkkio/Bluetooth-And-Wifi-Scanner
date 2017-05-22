var MockHandle = require('./mockHandle');
var mockHandle = new MockHandle();

class Dao {
  constructor(config) {
    Object.assign(this, config);

    this.dbHandle = this.useMockupData ? mockHandle : 'todo real DB handle';
  }

  getAllWifi() {
    var statement = 'SELECT * FROM WifiResults'
    this.dbHandle.prepare(statement);
    this.dbHandle.execute(statement);
    return this.dbHandle.fetch(statement);
  }

  getAllBt() {
    var statement = 'SELECT * FROM BluetoothResults'
    this.dbHandle.prepare(statement);
    this.dbHandle.execute(statement);
    return this.dbHandle.fetch(statement);
  }

  pingAzure() {

  }
  pingOkeanos() {

  }
  pingDigiOcean() {

  }
  countBt() {

  }
  countWifi() {

  }
}

module.exports = Dao;