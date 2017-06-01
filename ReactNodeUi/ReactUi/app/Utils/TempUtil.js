module.exports = {
    getResultType(result) {
        // Todo better way to define result type -> Compare against template model?
        return Object.keys(result).length === 6 ? 'wifi' : 'bluetooth';
    }
}