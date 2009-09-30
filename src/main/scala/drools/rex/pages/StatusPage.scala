package drools.rex.pages


import javax.servlet.ServletContext
import org.drools.agent.KnowledgeAgent

/**
 * 
 * @author Michael Neale
 */

object StatusPage {

  def render(kaList: List[KnowledgeAgent], ctx: ServletContext) = {
    <html>
      <head>
          <title>Drools Execution Server</title>
      </head>
      <body>
        <h1>Drools execution web server is running</h1>
        <h2>Configuration directory</h2>
        {ctx.getInitParameter("agent-config-directory")}
        <p>
        Use the above location to place changeset configuration files for the Knowledge Agent.
        </p>
        <h2>Knowledge Bases loaded:</h2>
        {kaList.map(ka => printKa(ka))}
      </body>
    </html>
  }


  private def printKa(ka: KnowledgeAgent) = {
    <h3>{ka.getName}</h3>
      <ul>
      <li>Number of packages: {ka.getKnowledgeBase.getKnowledgePackages.size}</li>
      </ul>
    
  }



}