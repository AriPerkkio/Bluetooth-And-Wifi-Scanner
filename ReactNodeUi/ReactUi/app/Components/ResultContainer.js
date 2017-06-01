var React     = require('react');
var PropTypes = require('prop-types');

var Table       = require('./Table');
var Content     = require('./Content');
var Loading     = require('./Loading');
var ArrayUtil   = require('../Utils/ArrayUtil');
var TestingUtil = require('../Utils/TestingUtil');

var Store            = require('../State/Store');
var { selectResult } = require('../State/Actions');

class ResultsContainer extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      results: []
    };
  }

  componentDidMount() {
    fetch(TestingUtil.getExtServerIp()+this.props.path)
      .then(response => response.json())
      .then(responseJson => this.setState({ 'results' : responseJson.sort(ArrayUtil.sortByName) }))
      .catch(error => console.log(error));
  }

  render() {
    return (
      <Content>
        { this.state.results.length ?
          <Table
            data={this.state.results}
            columns={Object.keys(this.state.results[0])}
            title={this.props.resultType+" results"} /> :
          <Loading text={"Loading "+this.props.resultType+" results..."}/>
        }
      </Content>
    )
  }
}

ResultsContainer.propTypes = {
  path: PropTypes.string.isRequired,
  resultType: PropTypes.string.isRequired
};

module.exports = ResultsContainer;