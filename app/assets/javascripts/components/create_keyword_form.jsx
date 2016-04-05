var CreateKeywordForm = React.createClass({
    getInitialState: function() {
        return {result: ""};
    },
    handleSubmit: function(e) {
        e.preventDefault();
        var url = $("#create-keyword-form").data("url");
        var keyword = ReactDOM.findDOMNode(this.refs.keyword).value.trim();
        $.ajax({
            url: url,
            dataType: "json",
            type: "POST",
            data: {keyword: keyword},
            success: function(data) {
                this.setState({result: "Finish!"});
            }.bind(this),
            error: function(xhr, status, err) {
                this.setState({result: "Error!"});
            }.bind(this)
        });
    },
    render: function() {
        return (
            <div>
                <div id="result">{this.state.result}</div>
                <form className="createKeywordForm" onSubmit={this.handleSubmit}>
                    <input type="text" placeholder="Keyword" ref="keyword"/>
                    <input type="submit" value="Create" />
                </form>
            </div>
        );
    }
});

React.render(
    <CreateKeywordForm />,
    document.getElementById("create-keyword-form")
);