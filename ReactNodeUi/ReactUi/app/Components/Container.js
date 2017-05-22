var React = require('react');

var MapComponent = require('./Map');
var Table = require('./Table');

function Content(props) {
    return (
        <section className={props.className}>
            {props.text}
            {props.children}
        </section>
    )
}

class Container extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      bluetoothResults: null,
      wifiResults: null,
      selectedResult: null
    }
  }

  componentDidMount() {
    [{path: 'getAllWifi', key: 'wifiResults'}, {path: 'getAllBt', key: 'bluetoothResults'}]
      .forEach((item) => {
        fetch('http://192.168.1.190:8081/'+item.path)
          .then((response) => response.json())
          .then((responseJson) => this.setState({ [item.key] : responseJson.sort(this.sortByName) }))
          .catch((error) => console.log(error));
      });
  }

  sortByName(a, b) {
    var nameA = a.name.toUpperCase().trim();
    var nameB = b.name.toUpperCase().trim();
    return nameA === '' || nameA === ' ' ? 1 :
           nameB === '' || nameB === ' ' ? -1 :
           nameA < nameB ? -1 :
           nameA > nameB ? 1 :
           0;
  }

  onResultClick(result) {
    this.setState({
      selectedResult: result
    })
  }

  render() {
    return (
      <main>
        <Content>
          { this.state.wifiResults ?
            <Table
              onResultClick={this.onResultClick.bind(this)}
              data={this.state.wifiResults}
              columns={Object.keys(this.state.wifiResults[0])}
              title="Wifi Results" /> :
            <p style={ {position: 'relative', textAlign: 'center', top: '50%'} }>
              Loading...
            </p>
          }
        </Content>

        <Content>
          <MapComponent markedObj={this.state.selectedResult} />
        </Content>

      </main>
    )
  }
}

module.exports = Container;


/*
this.state.bluetoothResults ?
            <Table
              onResultClick={this.onResultClick.bind(this)}
              data={this.state.bluetoothResults}
              columns={Object.keys(this.state.bluetoothResults[0])}
              title="Bluetooth Results" />

this.state.wifiResults ?
          <Table
            onResultClick={this.onResultClick.bind(this)}
            data={this.state.wifiResults}
            columns={Object.keys(this.state.wifiResults[0])}
            title="Wifi Results" />
*/