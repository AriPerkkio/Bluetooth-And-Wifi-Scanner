var React = require('react');

var Content         = require('./Content');
var Loading         = require('./Loading');
var Table           = require('./Table');
var MapContainer    = require('./MapContainer');
var ResultContainer = require('./ResultContainer');

var Store       = require('../State/Store');
var { connect } = require('react-redux');

class ComponentTester extends React.Component {

  render() {
    return (
      <Content>
        <div style={{display: 'flex'}}>
          <StoreContent />
          <ResultContainer path='/getAllBt' resultType='Bluetooth' />
        </div>
        <Content>
          <Table
            data={ [{keyOne: 'valueOne', keyTwo: 'valueTwo'}, {keyOne: 'valueThree', keyTwo: 'valueFour'}] }
            columns={ ['keyOne', 'keyTwo'] }
            title="ComponentTester" />
        </Content>

        <Content>
          <MapContainer />
        </Content>

      </Content>
    )
  }
}

const propsToState = store => ( { children: <pre> {JSON.stringify(store, null, 2) }</pre> });
const StoreContent = connect(propsToState)(Content);

module.exports = ComponentTester;