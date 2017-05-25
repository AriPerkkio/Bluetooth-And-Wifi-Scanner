var React     = require('react');
var PropTypes = require('prop-types');

function Loading(props) {
  var style = {
    position: 'relative',
    textAlign: 'center',
    top: '50%'
  };
  return <p style={style}>{props.text}</p>;
}

Loading.propTypes = {
  text: PropTypes.string
};

Loading.defaultProps = {
  text: 'Loading...'
};

module.exports = Loading;