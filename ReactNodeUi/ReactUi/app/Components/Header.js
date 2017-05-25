var React   = require('react');
var NavLink = require('react-router-dom').NavLink;

var Button = function(props) {
  return (
    <NavLink to={props.link}>
      <div className='button'>
        {props.text}
      </div>
    </NavLink>
  )
}

class Header extends React.Component {
  render() {
    return (
      <header>
        <Button text='Main'      link='/'/>
        <Button text='Bluetooth' link='/results/bt'/>
        <Button text='Wifi'      link='/results/wifi'/>
      </header>
    )
  }
}

module.exports = Header;