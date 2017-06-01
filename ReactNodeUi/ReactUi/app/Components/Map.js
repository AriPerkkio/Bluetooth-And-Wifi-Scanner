var React     = require('react');
var PropTypes = require('prop-types');

var Content = require('./Content');
var ArrayUtil = require('../Utils/ArrayUtil');

class Map extends React.Component {

  constructor(props) {
    super(props);
    this.markedResults = [];
  }

  setupMap(){
    this.map = new google.maps.Map(
      document.getElementById("googleMap"), {
        center:new google.maps.LatLng(51.508742,-0.120850),
        zoom:5,
    });
  }

  updateMarkers(props) {
    props.selectedResults.forEach(result => {
      if(ArrayUtil.includesResult(this.markedResults, result)) {
        return;
      }
      this.markedResults.push(result);

      var [lat, long] = result.loc.split(',')
        .map( item => item.trim() );

      var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat, long),
        animation: google.maps.Animation.DROP,
        map: this.map,
        icon: {
          url: '../app/Res/'+result.tech+'marker.png',
          scaledSize: new google.maps.Size(20, 20),
        }
      });

      var infowindow = new google.maps.InfoWindow({
        content: '<pre>'+JSON.stringify(result, null, 2)+'</pre>'
      });

      marker.addListener('click', () => infowindow.open(this.map, marker));
      this.map.setCenter(marker.getPosition());
      this.map.setZoom(10);
    });
  }

  componentWillReceiveProps(nextProps) {
    this.updateMarkers(nextProps);
  }

  componentDidMount() {
    this.setupMap();
    this.updateMarkers(this.props);
  }

  render() {
    return <Content className="map" id="googleMap"/>
  }
}

Map.propTypes = {
  selectedResults: PropTypes.arrayOf(PropTypes.object)
};

module.exports = Map;