var React = require('react');

class Map extends React.Component {

  componentWillReceiveProps(nextProps) {
    if(nextProps.markedObj) {
      var [lat, long] = nextProps.markedObj.loc.split(',')
        .map( (item) => item.trim() );

      var marker = new google.maps.Marker({
        position: new google.maps.LatLng(lat, long)
      });

      marker.setMap(this.map);
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
    return <div className='map' id='googleMap' />
  }
}

module.exports = Map;