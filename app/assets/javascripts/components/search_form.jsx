var SelectSortForm = React.createClass({
    onChange: function() {
        
    },
    render: function() {
        var options = this.props.sorts.map(function(sort) {
            return <option>{sort}</option>
        });
        return (
            <select name="sort" onChange={this.onChange} value={this.props.sorts}>
                {options}
            </select>
        );
    }
});

var SearchForm = React.createClass({
    render: function() {
        var sorts = ["created", "like", "rt"];
        return (
            <form className="searchForm" onSubmit={this.handleSubmit}>
                <input type="text" name="keyword"/>
                <SelectSortForm sorts={sorts} />
                <input type="date" name="since"/>
                <input type="submit" value="Search"/>
            </form>
        );
    }
});

ReactDOM.render(
    <SearchForm/>,
    document.getElementById("main")
);
