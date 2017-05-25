var React     = require('react');

var Content   = require('./Content');
var Loading   = require('./Loading');

class MainContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      countBt: null,
      countWifi: null
    };
  }

  componentDidMount() {
    setTimeout( () =>
    [ 'countBt', 'countWifi' ].forEach( key =>
      fetch('http://localhost:8081/'+key)
        .then((response) => response.text())
        .then((responseJson) => this.setState({ [key] : responseJson }))
        .catch((error) => console.log(error))
    )
    , 1500);
  }

  render() {
    return (
      <Content>
        { this.state.countBt ?
          <p className='count-text'>{this.state.countBt}</p> :
          <Loading text="Loading Bluetooth result count..."/>
        }
        { this.state.countWifi ?
          <p className='count-text'>{this.state.countWifi}</p> :
          <Loading text="Loading Wifi result count..."/>
        }
      </Content>
    )
  }
}

module.exports = MainContainer;