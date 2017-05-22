var React = require('react');
var PropTypes = require('prop-types');

var Textarea = (props) => (
  <textarea
    rows={1}
    cols={props.cols}
    placeholder={"Filter by " + props.placeholderName}
    className='tablefilter'/>
)

class Table extends React.Component {
  render() {
    return (
      <table className="results-table">
        <thead>
          <tr>
            <th colSpan={this.props.columns.length}>
              {this.props.title}
            </th>
          </tr>
          <tr>
            {this.props.columns.map( (item, idx) => 
            item !== 'loc' && 
            <th key={idx}>
              {item}
              <br/>
              <Textarea 
                cols={10}
                placeholderName={item}/>
            </th>
            )}
          </tr>
        </thead>
        <tbody>
          {this.props.data.map( (dataItem, dataIdx) =>
          <tr key={dataIdx} onClick={this.props.onResultClick.bind(null, dataItem)}>
            {this.props.columns.map( (colItem, colIdx) =>
            colItem !== 'loc' &&
            <td key={colIdx}>
              {dataItem[colItem]}
            </td>
            )}
          </tr>
          )}
        </tbody>
      </table>
    )
  }
}

Table.propTypes = {
  columns: PropTypes.array.isRequired,
  title: PropTypes.string.isRequired,
  data: PropTypes.arrayOf(PropTypes.object).isRequired,
  onResultClick: PropTypes.func.isRequired
};

module.exports = Table;