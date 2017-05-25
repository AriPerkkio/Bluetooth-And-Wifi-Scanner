var React     = require('react');
var PropTypes = require('prop-types');

var Content = require('./Content');

class Map extends React.Component {

  componentWillReceiveProps(nextProps) {
    if(nextProps.markedObj) {
      var [lat, long] = nextProps.markedObj.loc.split(',')
        .map( item => item.trim() );

      var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat, long),
        animation: google.maps.Animation.DROP,
        map: this.map,
      });

      var infowindow = new google.maps.InfoWindow({
        content: '<pre>'+JSON.stringify(nextProps.markedObj, null, 2)+'</pre>'
      });

      marker.addListener('click', () => infowindow.open(this.map, marker));
      this.map.setCenter(marker.getPosition());
    }
  }

  componentDidMount() {
    this.map = new google.maps.Map(
      document.getElementById("googleMap"), {
        center:new google.maps.LatLng(51.508742,-0.120850),
        zoom:5,
    });
  }

  render() {
    return <Content className="map" id="googleMap"/>
  }
}

Map.propTypes = {
  markedObj: PropTypes.object
};

module.exports = Map;