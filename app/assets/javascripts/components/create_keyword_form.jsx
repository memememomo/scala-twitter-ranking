var CreateKeywordForm = React.createClass({
    handleSubmit: function(e) {
        e.preventDefault();
        var keyword = ReactDOM.findDOMNode(this.refs.keyword).value.trim();
        alert(keyword);
    },
    render: function() {
        return (
            <form className="createKeywordForm" onSubmit={this.handleSubmit}>
                <input type="text" placeholder="Keyword" ref="keyword"/>
                <input type="submit" value="Create" />
            </form>
        );
    }
});

React.render(
    <CreateKeywordForm />,
    document.getElementById("create-keyword-form")
);