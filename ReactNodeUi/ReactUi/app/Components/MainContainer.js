var React = require('react');

var Content     = require('./Content');
var Loading     = require('./Loading');
var TestingUtil = require('../Utils/TestingUtil');

var Store        = require('../State/Store');
var { setCount } = require('../State/Actions');
var { connect }  = require('react-redux');

const countText = (props) => {
  if (props.text)
    return <p className='count-text'>{props.text}</p>;

  return <Loading text={'Loading '+ props.type +' result counts...'}/>;
}

class MainContainer extends React.Component {

  componentDidMount() {
    [ {key: 'bluetooth', path: '/countBt' }, {key: 'wifi', path: '/countWifi' } ]
      .forEach( opt =>
        fetch(TestingUtil.getExtServerIp()+opt.path)
          .then(response => response.text())
          .then(responseText => Store.dispatch(Object.assign({}, setCount, {resultCount: {[opt.key]:responseText} })))
          .catch(error => console.log(error))
    );
  }

  render() {
    return (
      <Content>
        <WifiCountText      type="Wifi"/>
        <BluetoothCountText type="Bluetooth"/>
      </Content>
    )
  }
}

const wifiStateToProps      = store => ( { text: store.counts.wifi } );
const bluetoothStateToProps = store => ( { text: store.counts.bluetooth } );

const WifiCountText      = connect(wifiStateToProps)(countText);
const BluetoothCountText = connect(bluetoothStateToProps)(countText);

module.exports = MainContainer;