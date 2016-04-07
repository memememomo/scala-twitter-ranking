var SelectSortForm = React.createClass({
    onChange: function() {
        
    },
    render: function() {
        var options = this.props.sorts.map(function(sort) {
            return <option value={sort}>{sort}</option>
        });
        return (
            <select name="sort" onChange={this.onChange}>
                {options}
            </select>
        );
    }
});

var SearchForm = React.createClass({
    render: function() {
        return (
            <form className="searchForm" onSubmit={this.handleSubmit}>
                <input type="text" name="keyword"/>
                <SelectSortForm/>
                <input type="date" name="since"/>
                <input type="submit" value="Search"/>
            </form>
        );
    }
});
