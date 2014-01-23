require 'buildr/git_auto_version'

desc 'GWT WebPoller Library'
define 'gwt-webpoller' do
  project.group = 'org.realityforge.gwt.webpoller'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache2_license
  pom.add_github_project('realityforge/gwt-webpoller')
  pom.add_developer('realityforge', "Peter Donald")
  pom.provided_dependencies.concat [:javax_annotation, :gwt_user]

  compile.with :javax_annotation, :gwt_user

  test.using :testng
  test.with :mockito

  package(:jar).include("#{_(:source, :main, :java)}/*")
  package(:sources)
  package(:javadoc)
end