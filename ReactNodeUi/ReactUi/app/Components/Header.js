var React = require('react');

var Button = function(props) {
    return (
        <div className='button'>
            {props.text}
        </div>
    )
}

class Header extends React.Component {
  render() {
    return (
      <header>
          <Button text='Menu'/>
          <Button text='About'/>
          <Button text='Contact'/>
      </header>
    )
  }
}

module.exports = Header;