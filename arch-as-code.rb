# Documentation: https://docs.brew.sh/Formula-Cookbook
#                https://rubydoc.brew.sh/Formula
# PLEASE REMOVE ALL GENERATED COMMENTS BEFORE SUBMITTING YOUR PULL REQUEST!
class ArchAsCode < Formula
  desc "Architecture as Code"
  homepage "https://tbd.io"
  url "https://github.com/nahknarmi/arch-as-code/releases/download/1.0.0/arch-as-code-1.0.tar.gz"
  sha256 "0fd11d35d307e4a28c2a68c5ac4e10418ede36a44a4ff8e344c0b2cae012bbbd"

  depends_on :java => "1.8+"

  bottle :unneeded           

  def install
    libexec.install Dir['*']                                              
    bin.write_exec_script Dir["#{libexec}/bin/arch-as-code"]    
  end

end
