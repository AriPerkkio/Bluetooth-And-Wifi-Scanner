var React     = require('react');
var PropTypes = require('prop-types');

var MapComponent = require('./Map');
var { connect }  = require('react-redux');

class MapContainer extends React.Component {

  render() {
    return <MapContent />
  }
}

const propsToState = store =>  ({ selectedResults: store.selectedResults });
const MapContent = connect(propsToState)(MapComponent);

module.exports = MapContainer;