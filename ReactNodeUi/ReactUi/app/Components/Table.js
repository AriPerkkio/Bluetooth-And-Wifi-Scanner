var React     = require('react');
var PropTypes = require('prop-types');

var debounce = require('debounce');

var Textarea = (props) => (
  <textarea
    rows={1}
    cols={props.cols}
    placeholder={"Filter by " + props.placeholderName}
    className='tablefilter'
    onChange={props.onChange}/>
)

class Table extends React.Component {

  constructor(props){
    super(props);

    this.setFilter = debounce(this.setFilter, 500);
    this.dataFilter = this.dataFilter.bind(this);
    this.state = {
      filters: {}
    };
  }

  onChangeHandler(column, e) {
    this.setFilter(column, e.target.value);
  }

  setFilter(filter, value) {
    var allFilters = this.state.filters;
    allFilters[ filter ] = value;
    this.setState({
      filters: allFilters
    });
  }

  dataFilter(item) {
    var matches = 1;

    Object.keys(this.state.filters).forEach(
      (filter) => {
        var filterValue = this.state.filters[ filter ];
        if( !item[filter].toLowerCase().includes(filterValue.toLowerCase()) ) {
          matches = 0;
        }
    });

    return matches;
  }

  render() {

    var filteredData = this.props.data.filter(this.dataFilter);

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
                placeholderName={item}
                onChange={this.onChangeHandler.bind(this, item)}/>
            </th>
            )}
          </tr>
        </thead>

        <tbody>
          {filteredData.map( (dataItem, dataIdx) =>
          <tr key={dataIdx} onClick={this.props.onResultClick.bind(null, dataItem)} >
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