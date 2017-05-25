var React     = require('react');
var PropTypes = require('prop-types');

function Content(props) {
  return (
    <section className={props.className} id={props.id}>
      {props.text}
      {props.children}
    </section>
  )
}

Content.propTypes = {
  className : PropTypes.string,
  id : PropTypes.string,
  text: PropTypes.string
};

module.exports = Content;